/* ==========================================================
   FundSpark — Dummy Data (simulates DB records)
   ========================================================== */

const FS_CATEGORIES = [
  {name:"Artificial Intelligence", icon:"brain-circuit", count:64},
  {name:"Healthcare", icon:"heart-pulse", count:48},
  {name:"Agriculture", icon:"sprout", count:31},
  {name:"Education", icon:"graduation-cap", count:39},
  {name:"FinTech", icon:"landmark", count:57},
  {name:"Robotics", icon:"bot", count:22},
  {name:"Food", icon:"utensils", count:28},
  {name:"Travel", icon:"plane", count:19},
  {name:"Gaming", icon:"gamepad-2", count:26},
  {name:"Cybersecurity", icon:"shield-check", count:33},
  {name:"E-commerce", icon:"shopping-bag", count:41},
  {name:"Renewable Energy", icon:"sun", count:35},
  {name:"IoT", icon:"cpu", count:24},
  {name:"SaaS", icon:"cloud", count:52},
  {name:"Blockchain", icon:"link-2", count:29},
  {name:"Social Impact", icon:"hand-heart", count:21}
];

const FS_STARTUPS = [
  {id:1, name:"AgroSense", founder:"Unknown", category:"Agriculture", score:92, goal:2500000, raised:1875000, days:18, rating:4.8, reviews:64, verified:true, trending:true, location:"Pune, MH", color:"#22C55E", tagline:"IoT soil sensors that help small farmers cut water use by 40%.", logo:"AS"},
  {id:2, name:"MediTrack AI", founder:"Aman Deshpande", category:"Healthcare", score:88, goal:5000000, raised:3120000, days:27, rating:4.6, reviews:41, verified:true, trending:true, location:"Bengaluru, KA", color:"#EF4444", tagline:"AI-assisted diagnostics for rural clinics without a radiologist on staff.", logo:"MT"},
  {id:3, name:"PayNest", founder:"Simran Kaur", category:"FinTech", score:95, goal:8000000, raised:7120000, days:6, rating:4.9, reviews:112, verified:true, trending:true, location:"Mumbai, MH", color:"#2563EB", tagline:"Micro-lending rails for gig workers, underwritten with UPI history.", logo:"PN"},
  {id:4, name:"ClassBridge", founder:"Neha Sharma", category:"Education", score:81, goal:1800000, raised:940000, days:34, rating:4.4, reviews:29, verified:false, trending:false, location:"Nagpur, MH", color:"#7C3AED", tagline:"Offline-first learning app for tier-3 schools with patchy internet.", logo:"CB"},
  {id:5, name:"RoboHarvest", founder:"Kunal Mehta", category:"Robotics", score:86, goal:6200000, raised:2480000, days:44, rating:4.5, reviews:37, verified:true, trending:false, location:"Ahmedabad, GJ", color:"#F59E0B", tagline:"Autonomous fruit-picking arms tuned for orchards under 5 acres.", logo:"RH"},
  {id:6, name:"CredSecure", category:"Cybersecurity", founder:"Ananya Iyer", score:90, goal:4000000, raised:3600000, days:11, rating:4.7, reviews:58, verified:true, trending:true, location:"Hyderabad, TS", color:"#0EA5E9", tagline:"Breach-detection for SMBs with zero in-house security team.", logo:"CS"},
  {id:7, name:"GreenVolt", founder:"Rohan Patil", category:"Renewable Energy", score:84, goal:9000000, raised:5040000, days:52, rating:4.3, reviews:22, verified:false, trending:false, location:"Nashik, MH", color:"#22C55E", tagline:"Modular rooftop solar kits installable by two people in a day.", logo:"GV"},
  {id:8, name:"KhetChain", founder:"Priya Nair", category:"Blockchain", score:79, goal:3000000, raised:960000, days:60, rating:4.1, reviews:16, verified:false, trending:false, location:"Kochi, KL", color:"#7C3AED", tagline:"Farm-to-fork traceability ledger for spice exporters.", logo:"KC"},
  {id:9, name:"ByteCart", founder:"Aditya Rao", category:"E-commerce", score:87, goal:2200000, raised:1980000, days:9, rating:4.6, reviews:73, verified:true, trending:true, location:"Pune, MH", color:"#EF4444", tagline:"Quick-commerce backend built for kirana stores under 500 sq ft.", logo:"BC"},
  {id:10, name:"NimbusStack", founder:"Vikram Joshi", category:"SaaS", score:93, goal:7500000, raised:6900000, days:4, rating:4.9, reviews:98, verified:true, trending:true, location:"Pune, MH", color:"#2563EB", tagline:"One-click infra for student developer teams shipping their first SaaS.", logo:"NS"},
  {id:11, name:"PlayForge", founder:"Ishaan Verma", category:"Gaming", score:75, goal:1500000, raised:410000, days:48, rating:4.0, reviews:12, verified:false, trending:false, location:"Chandigarh", color:"#F59E0B", tagline:"No-code engine for regional-language mobile games.", logo:"PF"},
  {id:12, name:"TasteRoute", founder:"Meera Pillai", category:"Food", score:82, goal:1200000, raised:840000, days:21, rating:4.5, reviews:44, verified:true, trending:false, location:"Chennai, TN", color:"#F59E0B", tagline:"Cloud kitchens built around regional Indian recipes, franchise-ready.", logo:"TR"}
];

const FS_TEAM = [
  {name:"Unknown", role:"Founder & CEO", init:"UK"},
  {name:"Shravani", role:"CTO", init:"SH"},
  {name:"Khushi", role:"Head of Ops", init:"KH"},
  {name:"Aarav", role:"Lead Engineer", init:"AB"}
];

const FS_COMMENTS = [
  {name:"Arjun Malhotra", init:"AM", time:"2 days ago", text:"Really impressed with the sensor accuracy in the demo video. What's the plan for after-sales support in remote districts?", likes:14},
  {name:"Sneha Kapoor", init:"SK", time:"4 days ago", text:"Invested already — the traction numbers from the pilot in Nashik are convincing. Excited to see the Q3 rollout.", likes:9},
  {name:"Wasim Sheikh", init:"WS", time:"1 week ago", text:"Curious how this compares to imported soil-sensor kits on cost per acre.", likes:5}
];

const FS_INVESTORS_LIST = [
  {name:"Arjun Malhotra", init:"AM", amount:250000, date:"18 Jul 2026", status:"Confirmed"},
  {name:"Sneha Kapoor", init:"SK", amount:180000, date:"15 Jul 2026", status:"Confirmed"},
  {name:"Wasim Sheikh", init:"WS", amount:90000, date:"10 Jul 2026", status:"Pending"},
  {name:"Divya Krishnan", init:"DK", amount:400000, date:"02 Jul 2026", status:"Confirmed"},
  {name:"Karan Bhatt", init:"KB", amount:60000, date:"28 Jun 2026", status:"Confirmed"}
];

const FS_NOTIFICATIONS = [
  {icon:"trending-up", color:"success", title:"New investment received", text:"Divya Krishnan invested ₹4,00,000 in AgroSense.", time:"10 min ago", unread:true},
  {icon:"message-circle", color:"primary", title:"New comment", text:"Wasim Sheikh commented on your campaign.", time:"1 hr ago", unread:true},
  {icon:"badge-check", color:"secondary", title:"Startup verified", text:"AgroSense has been verified by the admin team.", time:"3 hr ago", unread:true},
  {icon:"clock", color:"warning", title:"Campaign deadline approaching", text:"AgroSense campaign ends in 18 days.", time:"1 day ago", unread:false},
  {icon:"star", color:"warning", title:"New rating", text:"Your startup received a 5-star rating.", time:"2 days ago", unread:false}
];

const FS_USERS = [
  {name:"Unknown", init:"UK", email:"unknown@fundspark.io", role:"Founder", status:"Active", joined:"4 Aug 2026"},
  {name:"Arjun Malhotra", init:"AM", email:"arjun.m@fundspark.io", role:"Investor", status:"Active", joined:"03 Feb 2026"},
  {name:"Aman Deshpande", init:"AD", email:"aman.d@fundspark.io", role:"Founder", status:"Active", joined:"22 Feb 2026"},
  {name:"Sneha Kapoor", init:"SK", email:"sneha.k@fundspark.io", role:"Investor", status:"Suspended", joined:"11 Mar 2026"},
  {name:"Simran Kaur", init:"SimK", email:"simran.kaur@fundspark.io", role:"Founder", status:"Active", joined:"29 Mar 2026"},
  {name:"Wasim Sheikh", init:"WS", email:"wasim.s@fundspark.io", role:"Investor", status:"Pending", joined:"14 Apr 2026"}
];

const FS_PENDING_CAMPAIGNS = [
  {name:"UrbanRoots", founder:"Tanvi Oak", category:"Agriculture", goal:1600000, submitted:"28 Jul 2026"},
  {name:"CodeMint", founder:"Yash Kulkarni", category:"SaaS", goal:2100000, submitted:"27 Jul 2026"},
  {name:"SolarSprint", founder:"Naina Chopra", category:"Renewable Energy", goal:3400000, submitted:"25 Jul 2026"}
];

function fsCurrency(n){
  if(n>=10000000) return "₹" + (n/10000000).toFixed(2) + " Cr";
  if(n>=100000) return "₹" + (n/100000).toFixed(2) + " L";
  return "₹" + n.toLocaleString('en-IN');
}
function fsPct(raised,goal){ return Math.min(100, Math.round((raised/goal)*100)); }
